/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.felix.webconsole.plugins.obr.internal;


import java.io.IOException;
import java.net.URL;

import javax.servlet.ServletException;

import org.apache.felix.webconsole.AbstractWebConsolePlugin;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Version;
import org.osgi.service.obr.Capability;
import org.osgi.service.obr.Repository;
import org.osgi.service.obr.RepositoryAdmin;
import org.osgi.service.obr.Requirement;
import org.osgi.service.obr.Resolver;
import org.osgi.service.obr.Resource;


/**
 * This class provides a plugin for rendering the available OSGi Bundle Repositories
 * and the resources they provide.
 */
class OsgiBundleRepositoryRenderHelper extends AbstractBundleRepositoryRenderHelper
{

    OsgiBundleRepositoryRenderHelper( final AbstractWebConsolePlugin logger, final BundleContext bundleContext )
    {
        super( logger, bundleContext, RepositoryAdmin.class.getName() );
    }


    String getData( final String filter, final boolean details, Bundle[] bundles )
    {
        RepositoryAdmin admin = ( RepositoryAdmin ) getRepositoryAdmin();
        if ( admin != null )
        {
            JSONObject json = new JSONObject();
            try
            {
                json.put( "status", true ); //$NON-NLS-1$
                json.put( "details", details ); //$NON-NLS-1$

                final Repository repositories[] = admin.listRepositories();
                for ( int i = 0; repositories != null && i < repositories.length; i++ )
                {
                    json.append( "repositories", new JSONObject() //$NON-NLS-1$
                        .put( "lastModified", repositories[i].getLastModified() ) //$NON-NLS-1$
                        .put( "name", repositories[i].getName() ) //$NON-NLS-1$
                        .put( "url", repositories[i].getURL() ) ); //$NON-NLS-1$
                }

                Resource[] resources = admin.discoverResources( filter );
                for ( int i = 0; resources != null && i < resources.length; i++ )
                {
                    json.append( "resources", toJSON( resources[i], bundles, details ) ); //$NON-NLS-1$
                }
            }
            catch ( JSONException e )
            {
                logger.log( "Failed to serialize repository to JSON object.", e );
            }
            catch ( Exception e )
            {
                logger.log( "Failed to parse filter '" + filter + "'", e );
                try
                {
                    String reason = "filter=" + filter;
                    if ( e.getMessage() != null )
                    {
                        reason = e.getMessage() + "(" + reason + ")";
                    }
                    json.put( "error", reason ); //$NON-NLS-1$
                }
                catch ( JSONException je )
                {
                    // ignore
                }
            }

            return json.toString();
        }

        // fall back to no data
        return "{}"; //$NON-NLS-1$
    }

    void doAction( String action, String urlParam ) throws IOException, ServletException
    {
        RepositoryAdmin admin = ( RepositoryAdmin ) getRepositoryAdmin();
        Repository[] repos = admin.listRepositories();
        Repository repo = getRepository( repos, urlParam );

        URL uri = repo != null ? repo.getURL() : new URL( urlParam );

        if ( "delete".equals( action ) ) //$NON-NLS-1$
        {
            if ( !admin.removeRepository( uri ) )
            {
                throw new ServletException( "Failed to remove repository with URL " + uri );
            }
        }
        else if ( "add".equals( action ) || "refresh".equals( action ) ) //$NON-NLS-1$ //$NON-NLS-2$
        {
            try
            {
                admin.addRepository( uri );
            }
            catch ( IOException e )
            {
                throw e;
            }
            catch ( Exception e )
            {
                throw new ServletException( "Failed to " + action + " repository " + uri + ": " + e.toString() );
            }

        }
    }


    final void doDeploy( String[] bundles, boolean start, boolean optional )
    {
        // check whether we have to do something
        if ( bundles == null || bundles.length == 0 )
        {
            logger.log( "No resources to deploy" );
            return;
        }

        RepositoryAdmin repoAdmin = ( RepositoryAdmin ) getRepositoryAdmin();
        Resolver resolver = repoAdmin.resolver();

        // prepare the deployment
        for ( int i = 0; i < bundles.length; i++ )
        {
            String bundle = bundles[i];
            if ( bundle == null || bundle.equals( "-" ) )
            {
                continue;
            }

            String filter = "(id=" + bundle + ")";
            Resource[] resources = repoAdmin.discoverResources( filter );
            if ( resources != null && resources.length > 0 )
            {
                resolver.add( resources[0] );
            }
        }

        OsgiDeployer.deploy( resolver, logger, start );
    }


    private final Repository getRepository( Repository[] repos, String repositoryUrl )
    {
        if ( repositoryUrl == null || repositoryUrl.length() == 0 )
        {
            return null;
        }

        for ( int i = 0; i < repos.length; i++ )
        {
            if ( repositoryUrl.equals( repos[i].getURL().toString() ) )
            {
                return repos[i];
            }
        }

        return null;
    }


    private final JSONObject toJSON( Resource resource, Bundle[] bundles, boolean details ) throws JSONException
    {
        final String symbolicName = resource.getSymbolicName();
        final Version version = resource.getVersion();
        String installed = "";
        for ( int i = 0; symbolicName != null && installed.length() == 0 && bundles != null && i < bundles.length; i++ )
        {
            final Version ver = bundles[i].getVersion();
            if ( symbolicName.equals(bundles[i].getSymbolicName()))
            {
                installed = ver.toString();
            }
        }
        JSONObject json = new JSONObject()
            .put( "id", resource.getId() ) // //$NON-NLS-1$
            .put( "presentationname", resource.getPresentationName() ) // //$NON-NLS-1$
            .put( "symbolicname", symbolicName ) // //$NON-NLS-1$
            .put( "url", resource.getURL() ) // //$NON-NLS-1$
            .put( "version", version ) // //$NON-NLS-1$
            .put( "categories", resource.getCategories() ) // //$NON-NLS-1$
            .put( "installed", installed ); //$NON-NLS-1$

        if ( details )
        {
            Capability[] caps = resource.getCapabilities();
            for ( int i = 0; caps != null && i < caps.length; i++ )
            {
                json.append( "capabilities", new JSONObject() //$NON-NLS-1$
                    .put( "name", caps[i].getName() ) //$NON-NLS-1$
                    .put( "properties", new JSONObject( caps[i].getProperties() ) ) ); //$NON-NLS-1$
            }
            Requirement[] reqs = resource.getRequirements();
            for ( int i = 0; reqs != null && i < reqs.length; i++ )
            {
                json.append( "requirements", new JSONObject() //$NON-NLS-1$
                    .put( "name", reqs[i].getName() ) //$NON-NLS-1$
                    .put( "filter", reqs[i].getFilter() ) //$NON-NLS-1$
                    .put( "optional", reqs[i].isOptional() ) ); //$NON-NLS-1$
            }

            final RepositoryAdmin admin = ( RepositoryAdmin ) getRepositoryAdmin();
            Resolver resolver = admin.resolver();
            resolver.add( resource );
            resolver.resolve(); // (Resolver.NO_OPTIONAL_RESOURCES);
            Resource[] required = resolver.getRequiredResources();
            for ( int i = 0; required != null && i < required.length; i++ )
            {
                json.append( "required", toJSON( required[i], bundles, false ) ); //$NON-NLS-1$
            }
            Resource[] optional = resolver.getOptionalResources();
            for ( int i = 0; optional != null && i < optional.length; i++ )
            {
                json.append( "optional", toJSON( optional[i], bundles, false ) ); //$NON-NLS-1$
            }
            Requirement/*Reason*/[] unsatisfied = resolver.getUnsatisfiedRequirements();
            for ( int i = 0; unsatisfied != null && i < unsatisfied.length; i++ )
            {
                json.append( "unsatisfied", new JSONObject() //$NON-NLS-1$
                    .put( "name", unsatisfied[i].getName() ) //$NON-NLS-1$
                    .put( "filter", unsatisfied[i].getFilter() ) //$NON-NLS-1$
                    .put( "optional", unsatisfied[i].isOptional() ) ); //$NON-NLS-1$
            }
        }
        return json;
    }

}
